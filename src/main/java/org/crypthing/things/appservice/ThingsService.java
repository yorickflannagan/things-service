package org.crypthing.things.appservice;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.ObjectName;

import org.crypthing.things.snmp.EncodableString;
import org.crypthing.things.snmp.LifecycleEvent;
import org.crypthing.things.snmp.ProcessingEventListener;
import org.crypthing.things.snmp.SNMPBridge;
import org.crypthing.things.snmp.SignalBean;
import org.crypthing.things.snmp.LifecycleEvent.LifecycleEventType;
import org.crypthing.things.snmp.ProcessingEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.json.JSONObject;

import io.quarkus.runtime.Startup;

/**
 * Implementação de logging remoto e monitoramento.
 * É esperado que o serviço seja mantido em referência que dure por todo o ciclo de vida
 * do endpoint, numa variável de instância.
 *
 * @author magut
 * @version $Id: $Id
 */
@Singleton
@Startup
public class ThingsService implements ThingsServiceMBean, ProcessingEventListener
{

	private long success;
	private long error;
	private final String[] environ;
	private SNMPBridge bridge;
	private ObjectName serviceName;

	@ConfigProperty(name = "org.crypthing.things.MIBAddress", defaultValue = "127.0.0.1/8163")
	String mibAddress;
	@ConfigProperty(name = "org.crypthing.things.rootOID", defaultValue = "1.51.171")
	String rootOID;
	@ConfigProperty(name = "org.crypthing.things.jmxHost", defaultValue = "127.0.0.1")
	String jmxHost;
	@ConfigProperty(name = "org.crypthing.things.jmxPort", defaultValue = "8001")
	String jmxPort;
	@ConfigProperty(name = "org.crypthing.things.endpoint", defaultValue = "service")
	String endpoint;
	@ConfigProperty(name = "org.crypthing.things.heartbeat", defaultValue = "5000")
	long heartbeat;

	/**
	 * Cria uma nova instância do serviço. A utilização dos métodos do serviço requer a execução do
	 * método init() antes de qualquer outro método.
	 */
	public ThingsService()
	{
		final Map<String, String> map = System.getenv();
		final int size = map.size();
		final ArrayList<String> env = new ArrayList<String>(size);
		final Iterator<String> keys = map.keySet().iterator();
		while (keys.hasNext())
		{
			final String key = keys.next();
			env.add(key + "=" + map.get(key));
		}
		final String[] ret = new String[size];
		environ = env.toArray(ret);
		success = 0;
		error = 0;
		bridge = null;
	}


	/**
	 * Inicialização do serviço. Método executado pelo container
	 */
	@PostConstruct
	public void start() 
	{
		try
		{
			bridge = SNMPBridge.newInstance("org.crypthing.things.snmp.SNMPBridge", mibAddress, rootOID);
			serviceName = new ObjectName
			(
				(new StringBuilder(256))
				.append(Runner.MBEAN_PATTERN)
				.append(endpoint)
				.toString()
			);
			ManagementFactory.getPlatformMBeanServer().registerMBean(this, serviceName);
			final Thread daemon = new Thread(new Heartbeat());
			daemon.setDaemon(true);
			daemon.start();
			bridge.notify(new LifecycleEvent(LifecycleEventType.start, new SignalBean(endpoint, "Service started").encode()));
		}
		catch (final Throwable e) { throw new ServiceInitException(e); }
	}
	/**
	 * Finalização do serviço. Método executado pelo container.
	 */
	@PreDestroy
	public void stop()
	{
		bridge.notify(new LifecycleEvent(LifecycleEventType.stop, new SignalBean(endpoint, "Service stoped").encode()));
		heartbeat = 0;
		try { ManagementFactory.getPlatformMBeanServer().unregisterMBean(serviceName); }
		catch (final MBeanRegistrationException | InstanceNotFoundException swallowed) {}
	}

	/**
	 * {@inheritDoc}
	 *
	 * Interface para conexão JMX. Utilizada exclusivamente pelo monitor e não pelas aplicações.
	 */
	@Override public void shutdown() {}
	/**
	 * {@inheritDoc}
	 *
	 * Interface para conexão JMX. Utilizada exclusivamente pelo monitor e não pelas aplicações.
	 */
	@Override public void shutdown(final String key) {}
	/**
	 * {@inheritDoc}
	 *
	 * Interface para conexão JMX. Utilizada exclusivamente pelo monitor e não pelas aplicações.
	 */
	@Override public int getWorkerCount() { return 1; }
	/**
	 * {@inheritDoc}
	 *
	 * Interface para conexão JMX. Utilizada exclusivamente pelo monitor e não pelas aplicações.
	 */
	@Override public String getConfigFile() { return ""; }
	/**
	 * {@inheritDoc}
	 *
	 * Interface para conexão JMX. Utilizada exclusivamente pelo monitor e não pelas aplicações.
	 */
	@Override public long getSuccessCount() { return success; }
	/**
	 * {@inheritDoc}
	 *
	 * Interface para conexão JMX. Utilizada exclusivamente pelo monitor e não pelas aplicações.
	 */
	@Override public long getErrorCount() { return error; }
	/**
	 * {@inheritDoc}
	 *
	 * Interface para conexão JMX. Utilizada exclusivamente pelo monitor e não pelas aplicações.
	 */
	@Override public String[] getEnvironment() { return environ; }


	/**
	 * {@inheritDoc}
	 *
	 * Implementação de logging remoto ao nível de info.
	 */
	@Override public void info(final ProcessingEvent e) { bridge.notify(e); }
	/**
	 * {@inheritDoc}
	 *
	 * Implementação de logging remoto ao nível de warning
	 */
	@Override public void warning(final ProcessingEvent e) { bridge.notify(e); }
	/**
	 * {@inheritDoc}
	 *
	 * Implementação de logging remoto ao nível de info
	 */
	@Override public void error(final ProcessingEvent e) { bridge.notify(e); }


	/**
	 * Incrementar contador de sucessos. Executar sempre que um ciclo de atendimento terminar e
	 * tiver sido realizado com sucesso.
	 */
	public void success() { success++; }
	/**
	 * Incrementar contador de falhas. Executar sempre que um ciclo de atendimento terminar
	 * e tiver sido encerrado com falha.
	 */
	public void failure() { error++; }


	private class Heartbeat implements Runnable
	{
		private final JSONObject jmx;
		private Heartbeat()
		{
			jmx = new JSONObject();
			jmx.put("address", jmxHost);
			jmx.put("port", jmxPort);
		}
		@Override
		public void run()
		{
			while (heartbeat > 0)
			{
				final JSONObject json = new JSONObject();
				json.put("jmx", jmx);
				json.put("success", success);
				json.put("failures", error);
				json.put("workers", getWorkerCount());
				bridge.notify(new LifecycleEvent(LifecycleEventType.heart, new EncodableString(json.toString())));
				try{ Thread.sleep(heartbeat); } catch(final InterruptedException swallowed) {}
			}
		}
	}
}
