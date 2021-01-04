package org.crypthing.things.appservice;

/**
 * Exceção lançada caso não seja possível inicializar o serviço ThingsService
 *
 * @author magut
 * @version $Id: $Id
 */
public class ServiceInitException extends RuntimeException
{
	private static final long serialVersionUID = -3059195833417231592L;
	/**
	 * <p>Constructor for ServiceInitException.</p>
	 *
	 * @param e a {@link java.lang.Throwable} object.
	 */
	public ServiceInitException(final Throwable e) { super(e); }
}
