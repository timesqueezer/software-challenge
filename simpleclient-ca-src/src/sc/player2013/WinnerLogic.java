package sc.player2013;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import sc.plugin2013.BackwardMove;
import sc.plugin2013.Board;
import sc.plugin2013.Field;
import sc.plugin2013.FieldType;
import sc.plugin2013.ForwardMove;
import sc.plugin2013.GameState;
import sc.plugin2013.IGameHandler;
import sc.plugin2013.Move;
import sc.plugin2013.MoveContainer;
import sc.plugin2013.Pirate;
import sc.plugin2013.Player;
import sc.plugin2013.PlayerColor;
import sc.plugin2013.util.InvalidMoveException;
import sc.shared.GameResult;

/**
 * Das Herz des Simpleclients: Eine sehr simple Logik, die ihre Zuege zufaellig
 * waehlt, aber gueltige Zuege macht. Ausserdem werden zum Spielverlauf
 * Konsolenausgaben gemacht.
 */
public class WinnerLogic implements IGameHandler {

	private Starter client;
	private GameState gameState;
	private Player currentPlayer;

	/*
	 * Klassenweit verfuegbarer Zufallsgenerator der beim Laden der klasse
	 * einmalig erzeugt wird und dann immer zur Verfuegung steht.
	 */
	private static final Random rand = new SecureRandom();

	/**
	 * Erzeugt ein neues Strategieobjekt, das zufaellige Zuege taetigt.
	 * 
	 * @param client
	 *            Der Zugrundeliegende Client der mit dem Spielserver
	 *            kommunizieren kann.
	 */
	public WinnerLogic(Starter client) {
		this.client = client;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void gameEnded(GameResult data, PlayerColor color,
			String errorMessage) {

		System.out.println("*** Das Spiel ist beendet");
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getFirstPirateFieldIndex() {
		int result = 0;
		for (int k = 0; k < gameState.getBoard().size(); k++) {
			if (gameState.getBoard().getField(k).numPirates(gameState.getCurrentPlayerColor()) > 0) {
				result = k;	
				break;
			}
		}
		return result;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onRequestAction() {
		System.out.println("*** Es wurde ein Zug angefordert");
		MoveContainer moveC = new MoveContainer();
		// Schleife die 3 mal durchlaufen wird. i wird jedes mal erhöht
		for (int i = 0; i < 3; i++) {
			// Liste der verfügbaren Züge
			LinkedList<Move> possibleMoves = (LinkedList<Move>) gameState.getPossibleMoves();
			List<ForwardMove> possibleForwardMoves = new ArrayList<ForwardMove>();
			List<BackwardMove> possibleBackwardMoves = new ArrayList<BackwardMove>();
			for (Move m: possibleMoves) {
				if (m.getClass().equals(ForwardMove.class)) {
					possibleForwardMoves.add((ForwardMove) m);
				} else if (m.getClass().equals(BackwardMove.class)) {
					possibleBackwardMoves.add((BackwardMove) m);
				}
			}
			System.out.println("*** Anzahl der möglichen Züge:"
					+ possibleMoves.size());
			
			Move move = null;
			
			// Wenn es mögliche Züge gibt:
			if (possibleMoves.size() > 0) {
				if (gameState.getCurrentPlayer().getCards().size() <= 1) {
					for (int k = gameState.getBoard().size(); k > 0; k--) {
						System.out.println("Size:" + k);
						Field pirate_field = gameState.getBoard().getField(k-1);
						if (pirate_field.numPirates(gameState.getCurrentPlayerColor()) > 0) {
							move = new BackwardMove(k-1);
							break;
						}
					}
					
					
				} else {
					int k = getFirstPirateFieldIndex();
					System.out.println("K = " + k);
					for (Move m: possibleForwardMoves) {
						if (m.fieldIndex == k) {
							move = (ForwardMove) m;
							break;
						}	
					}
				}	

			} else {
				move = null;
			}
			// Hinzufügen des Zuges zum Container
			moveC.addMove(move);
			// Lokalen GameState auf den Move aktualisieren.
			if (move != null) {
				try {
					// Führt den Teilzug aus, hier wird überprüft ob der Zug
					// gültig ist.
					move.perform(gameState, gameState.getCurrentPlayer());
					// Aktualisiert die Punktzahl
					gameState.prepareNextTurn(move);
				} catch (InvalidMoveException e) {
					System.out.println("*** Ungültiger Zug ausgeführt");
					e.printStackTrace();
				}
			}
		}

		// Sende den Container mit allen durchgeführten Zügen.
		sendAction(moveC);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onUpdate(Player player, Player otherPlayer) {
		currentPlayer = player;

		System.out.println("*** Spielerwechsel: " + player.getPlayerColor());

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onUpdate(GameState gameState) {
		this.gameState = gameState;
		currentPlayer = gameState.getCurrentPlayer();

		System.out.print("*** Das Spiel geht vorran: Zug = "
				+ gameState.getTurn());
		System.out.println(", Spieler = " + currentPlayer.getPlayerColor());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendAction(MoveContainer move) {
		client.sendMove(move);
	}

}
