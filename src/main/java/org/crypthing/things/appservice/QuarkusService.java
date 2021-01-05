package org.crypthing.things.appservice;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.crypthing.things.snmp.ProcessingEvent;
import org.crypthing.things.snmp.SignalBean;
import org.crypthing.things.snmp.ProcessingEvent.ProcessingEventType;

/**
 * Aplicação de teste e demonstração. Compilada exclusivamente em debug
 */
@Path("/service")
public class QuarkusService
{
	@Inject
	ThingsService service;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
	public Response getSomething(@QueryParam("type") int type)
	{
		Response.Status status;
		String entity;
		try
		{
			if (type == 0) throw new RuntimeException();
			if (type == 1)
			{
				// Ocorreu uma falha de negócio: log warning
				status = Response.Status.BAD_REQUEST;
				entity = "Falha na validação do request";
				service.warning(new ProcessingEvent(ProcessingEventType.warning, new SignalBean("test", entity).encode()));
			}
			else
			{
				// Operação de negócio bem sucedida: log info (se for o caso)
				status = Response.Status.OK;
				entity = "Operação bem sucedida";
				service.info(new ProcessingEvent(ProcessingEventType.info, new SignalBean("test", entity).encode()));
			}
		}
		catch (final Exception e)
		{
			// Erro interno: log error
			status = Response.Status.INTERNAL_SERVER_ERROR;
			entity = "Ocorreu um erro na execução";
			service.error(new ProcessingEvent(ProcessingEventType.error, entity, e));
		}

		// Bilhete a operação
		if (status == Response.Status.OK) service.success();
		else service.failure();

		return Response.status(status).entity(entity).build();
    }
}
