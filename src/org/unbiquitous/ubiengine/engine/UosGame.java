package org.unbiquitous.ubiengine.engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListResourceBundle;

import org.unbiquitous.ubiengine.engine.input.keyboard.KeyboardManager;
import org.unbiquitous.ubiengine.engine.input.keyboard.KeyboardReceptionDriver;
import org.unbiquitous.ubiengine.engine.input.mouse.MouseManager;
import org.unbiquitous.ubiengine.engine.time.DeltaTime;
import org.unbiquitous.ubiengine.util.ComponentContainer;
import org.unbiquitous.ubiengine.util.Logger;
import org.unbiquitous.uos.core.UOS;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.applicationManager.UosApplication;
import org.unbiquitous.uos.core.ontologyEngine.api.OntologyDeploy;
import org.unbiquitous.uos.core.ontologyEngine.api.OntologyStart;
import org.unbiquitous.uos.core.ontologyEngine.api.OntologyUndeploy;
import org.unbiquitous.uos.network.socket.connectionManager.TCPConnectionManager;
import org.unbiquitous.uos.network.socket.radar.PingRadar;

/**
 * The game class. Extend it only to implement getSettings().
 * @author Pimenta
 *
 */
public abstract class UosGame implements UosApplication {
  /**
   * Must be implemented by the game class.
   * @return Reference to the game initial settings.
   */
  protected abstract Settings getSettings();
  
  /**
   * Use this method in main() to start the game.
   * @param game Class{@literal <}?{@literal >} that extends UosGame.
   */
  public static void run(final Class<?> game) {
    new UOS().init(new ListResourceBundle() {
      protected Object[][] getContents() {
        return new Object[][] {
          {"ubiquitos.connectionManager", TCPConnectionManager.class.getName()},
          {"ubiquitos.radar", PingRadar.class.getName()},
          {"ubiquitos.eth.tcp.port", "14984"},
          {"ubiquitos.eth.tcp.passivePortRange", "14985-15000"},
          {"ubiquitos.uos.deviceName","compDevice"},
          {"ubiquitos.driver.deploylist", KeyboardReceptionDriver.class.getName()},
          {"ubiquitos.application.deploylist", game.getName()}
        };
      }
    });
  }
  
  /**
   * Just a "typedef" for HashMap{@literal <}String, Object{@literal >}.
   * @author Pimenta
   *
   */
  @SuppressWarnings("serial")
  public class Settings extends HashMap<String, Object> {
    private Settings validate() {
      if (get("root_path") == null)
        put("root_path", ".");
      if (get("window_title") == null)
        put("window_title", "UbiEngine");
      if (get("window_width") == null)
        put("window_width", 1280);
      if (get("window_height") == null)
        put("window_height", 720);
      if (get("root_path") == null)
        throw new Error("First game state not defined!");
      return this;
    }
  }
  
  private ComponentContainer components = new ComponentContainer();
  private LinkedList<GameState> states = new LinkedList<GameState>();
  private Screen screen;
  private DeltaTime deltatime;
  private KeyboardManager keyboard_manager;
  private MouseManager mouse_manager;
  private Settings settings;
  
  private void init(Gateway gateway) {
    settings = getSettings().validate();
    components.put(Settings.class, settings);
    
    components.put(UosGame.class, this);
    
    components.put(Gateway.class, gateway);
    
    screen = new Screen(
        (String)settings.get("window_title"),
        ((Integer)settings.get("window_width")).intValue(),
        ((Integer)settings.get("window_height")).intValue(),
        deltatime
    );
    components.put(Screen.class, screen);
    
    keyboard_manager = new KeyboardManager(components);
    components.put(KeyboardManager.class, keyboard_manager);
    
    mouse_manager = new MouseManager(components);
    components.put(MouseManager.class, mouse_manager);
    
    try {
      states.add(
        ((GameState)((Class<?>)settings.get("first_state")).newInstance())
        .setComponents(components)
      );
    } catch (Exception e) {
      throw new Error(e.getMessage());
    }
  }
  
  private void close() {
    screen.close();
  }
  
  private void run() {
    while (states.size() > 0) {
      deltatime.start();
      update();
      render();
      deltatime.finish();
      checkStateChange();
    }
  }
  
  private void update() {
    for (GameState gs : states)
      gs.update();
  }
  
  private void render() {
    for (GameState gs : states)
      gs.render();
    screen.update();
  }
  
  private enum ChangeOption {
    NA,
    CHANGE,
    PUSH,
    POP
  }
  
  private GameState state_change = null;
  private Object[] pop_args = null;
  private ChangeOption change_option = ChangeOption.NA;
  
  private void checkStateChange() {
    switch (change_option) {
      case NA:
        break;
      
      case CHANGE:
        states.removeLast().close();
        states.add(state_change);
        break;
        
      case PUSH:
        states.add(state_change);
        break;
        
      case POP:
        states.removeLast().close();
        if (states.size() > 0)
          states.getLast().wakeup(pop_args);
        break;
        
      default:
        throw new Error("Invalid value for ChangeOption in UosGame!");
    }
    state_change = null;
    pop_args = null;
    change_option = ChangeOption.NA;
  }
  
  public void change(GameState state) {
    state_change = state;
    change_option = ChangeOption.CHANGE;
  }
  
  public void push(GameState state) {
    state_change = state;
    change_option = ChangeOption.PUSH;
  }
  
  public void pop(Object... args) {
    pop_args = args;
    change_option = ChangeOption.POP;
  }
  
  public <T> T build(Class<T> key, Object... args) {
    T tmp = null;
    // if (key == Sprite.class) FIXME
    return tmp;
  }
  
  public void start(Gateway gateway, OntologyStart ontology) {
    try {
      init(gateway);
      run();
    } catch (Error e) {
      String path;
      try {
        path = (String)settings.get("root_path");
      } catch (Throwable e1) {
        path = ".";
      }
      Logger.log(e, path + "/ErrorLog.txt");
    }
  }
  
  public void stop() {
    close();
  }
  
  public void init(OntologyDeploy ontology, String appId) {
    
  }
  
  public void tearDown(OntologyUndeploy ontology) {
    
  }
}
