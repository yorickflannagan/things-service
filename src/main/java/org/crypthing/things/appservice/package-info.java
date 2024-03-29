/**
 * Implementação de logging remoto e monitoramento. Veja o seguinte exemplo:
 * <pre>
 * // Declare o serviço (um singleton)
 * {@literal @}Inject
 * ThingsService service;
 * 
 * // Onde seja necessário utilizá-lo, faça:
 * {@literal @}GET
 * {@literal @}Produces(MediaType.TEXT_PLAIN)
 * public Response getSomething()
 * {
 * 	Response.Status status;
 * 	String entity;
 *  boolean valid;
 * 	try
 * 	{
 * 		// Executa seja lá o que for
 * 		if (!valid)
 * 		{
 * 			// Ocorreu uma falha de negócio: log warning
 * 			status = Response.Status.BAD_REQUEST;
 * 			entity = "Falha na validação do request";
 * 			service.warning(new ProcessingEvent(ProcessingEventType.warning, new SignalBean("test", entity).encode()));
 * 		}
 * 		else
 * 		{
 * 			// Operação de negócio bem sucedida: log info (se for o caso)
 * 			status = Response.Status.OK;
 * 			entity = "Operação bem sucedida";
 * 			service.info(new ProcessingEvent(ProcessingEventType.info, new SignalBean("test", entity).encode()));
 * 		}
 * 	}
 * 	catch (final Exception e)
 * 	{
 * 		// Erro interno: log error
 * 		status = Response.Status.INTERNAL_SERVER_ERROR;
 * 		entity = "Ocorreu um erro na execução";
 * 		service.error(new ProcessingEvent(ProcessingEventType.error, entity, e));
 * 	}
 * 
 * 	// Bilhete a operação
 * 	if (status == Response.Status.OK) service.success();
 * 	else service.failure();
 * 
 * 	return Response.status(status).entity(entity).build();
 * }
 * </pre>
 */
package org.crypthing.things.appservice;
