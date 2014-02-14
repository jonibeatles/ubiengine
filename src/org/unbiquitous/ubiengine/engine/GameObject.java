package org.unbiquitous.ubiengine.engine;

import org.unbiquitous.ubiengine.util.ComponentContainer;

/**
 * Use this class to implement game objects of a ContainerGameState.
 * @author Pimenta
 *
 */
public abstract class GameObject {
  /**
   * Use to manage singleton instances.
   */
  protected ComponentContainer components;
  
  /**
   * Method to implement update.
   */
  public abstract void update();
  
  /**
   * Method to implement rendering.
   */
  public abstract void render();
  
  /**
   * Handle a pop from the stack of game states.
   * @param args Arguments passed from the state popped.
   */
  public abstract void wakeup(Object... args);
  
  /**
   * Method to close whatever is necessary.
   */
  public abstract void close();
  
  /**
   * Create a new game state and pass to this method to change the current
   * game state.
   * @param state The new game state.
   */
  protected void change(GameState state) {
    game.change(state.setComponents(components));
  }
  
  /**
   * Create a new game state and pass to this method to push it.
   * @param state The new game state.
   */
  protected void push(GameState state) {
    game.push(state.setComponents(components));
  }
  
  /**
   * Create arguments (or not) and pass to this method to pop this game state.
   * @param args Args to be passed to the new state in the top of the stack.
   */
  protected void pop(Object... args) {
    game.pop(args);
  }
  
  /**
   * Use this method to build assets.
   * @param key Class of the asset to be created.
   * @param args Arguments to be passed to the constructor.
   * @return Asset reference.
   */
  protected <T> T build(Class<T> key, Object... args) {
    return game.build(key, args);
  }
  
  /**
   * Method to set the game components.
   * @param coms Components reference.
   * @return This.
   */
  public GameObject setComponents(ComponentContainer coms) {
    components = coms;
    game = coms.get(UosGame.class);
    return this;
  }
  
  private UosGame game;
}
