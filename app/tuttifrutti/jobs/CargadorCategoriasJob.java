package tuttifrutti.jobs;

/**
 * @author rfanego
 */
public class CargadorCategoriasJob implements Runnable {

	@Override
	public void run() {
		/* TODO implementar
		 * Carga palabras de categorías desde elastic hacia una cache (Redis? EhCache?)
		 * Deberían guardar en una key del tipo CATEGORIA-LETRA, una cantidad N de palabras.
		 * Esta Cache sería utilizada para los powerups en las Partidas.
		 * Cada Partida haría una copia de las categorías que usen, con una key del tipo
		 * CATEGORIA-LETRA-IdPARTIDA
		 * Este u otro job, debería encargarse eliminar estas keys asociadas a cada partido.
		 */
	}
}
