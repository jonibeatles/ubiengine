package org.unbiquitous.ubiengine.engine.core;

import org.unbiquitous.ubiengine.engine.asset.AssetManager;

/**
 * Interface for a game state.
 * @see ContainerState
 * @author Pimenta
 *
 */
public abstract class GameState {
  /**
   * Use to load assets.
   */
  protected AssetManager assets = new AssetManager();
  
  /**
   * Method to implement update.
   */
  protected abstract void update();
  
  /**
   * Method to implement rendering.
   */
  protected abstract void render();
  
  /**
   * Handle a pop from the stack of game states.
   * @param args Arguments passed from the state popped.
   */
  protected abstract void wakeup(Object... args);
  
  /**
   * Method to close whatever is necessary.
   */
  protected abstract void close();
}
