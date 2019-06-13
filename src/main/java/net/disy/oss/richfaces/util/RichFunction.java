package net.disy.oss.richfaces.util;

import java.util.Iterator;

import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 * This class contains methods which substitutes some equivalent functions from RichFunctions.
 *
 */
public class RichFunction {
  private RichFunction() {}

  /**
   * This function returns the client identifier related to the passed component identifier ('id'). If the specified component identifier is not found, null is returned instead.
   *
   * @param id component identifier
   * @return
   */
  public static String clientId(String id) {
    FacesContext context = FacesContext.getCurrentInstance();
    UIComponent component = findComponent(context, id);
    return component != null ? component.getClientId(context) : null;
  }

  private static UIComponent findComponent(FacesContext context, String id) {
    if (id != null) {
      UIComponent contextComponent = UIComponent.getCurrentComponent(context);
      if (contextComponent == null) {
        contextComponent = context.getViewRoot();
      }

      UIComponent component = findComponentFor(contextComponent, id);
      if (component != null) {
        return component;
      }
    }

    return null;
  }

  /**
   * <p>A modified JSF alghoritm for looking up components.</p>
   *
   * <p>First try to find the component with given ID in subtree and then lookup in parents' subtrees.</p>
   *
   * <p>If no component is found this way, it uses {@link #findUIComponentBelow(UIComponent, String)} applied to root component.</p>
   *
   * @param component
   * @param id
   * @return
   */
  private static UIComponent findComponentFor(UIComponent component, String id) {
    if (id == null) {
      throw new NullPointerException("id is null!");
    }

    if (id.length() == 0) {
      return null;
    }

    UIComponent target = null;
    UIComponent parent = component;
    UIComponent root = component;

    while ((null == target) && (null != parent)) {
      target = parent.findComponent(id);
      root = parent;
      parent = parent.getParent();
    }

    if (null == target) {
      target = findUIComponentBelow(root, id);
    }

    return target;
  }

  /**
   * Looks up component with given ID in subtree of given component including all component's chilren, component's facets and subtrees under naming containers.
   */
  private static UIComponent findUIComponentBelow(UIComponent root, String id) {
    UIComponent target = null;

    for (Iterator<UIComponent> iter = root.getFacetsAndChildren(); iter.hasNext();) {
      UIComponent child = iter.next();

      if (child instanceof NamingContainer) {
        try {
          target = child.findComponent(id);
        } catch (IllegalArgumentException iae) {
          continue;
        }
      }

      if (target == null) {
        if ((child.getChildCount() > 0) || (child.getFacetCount() > 0)) {
          target = findUIComponentBelow(child, id);
        }
      }

      if (target != null) {
        break;
      }
    }

    return target;
  }
}
