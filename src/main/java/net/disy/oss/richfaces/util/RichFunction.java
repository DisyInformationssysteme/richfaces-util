package net.disy.oss.richfaces.util;

import java.util.Arrays;
import java.util.Iterator;

import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 * This class contains methods which substitutes some equivalent functions from RichFunctions.
 *
 */
public class RichFunction {
  // there is the same pattern in client-side code:
  // richfaces.js - RichFaces.escapeCSSMetachars(s)
  private static final char[] CSS_SELECTOR_CHARS_TO_ESCAPE = createSortedCharArray(
      "#;&,.+*~':\"!^$[]()=>|/");

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

  /**
   * The rich:jQuerySelector('id') function will perform nearly the same function as rich:clientId('id') but will transform
   * the resulting id into a jQuery id selector which means that it will add a "#" character at the beginning and escape all
   * reserved characters in CSS selectors.
   */
  public static String jQuerySelector(String id) {
    FacesContext facesContext = FacesContext.getCurrentInstance();
    UIComponent component = findComponent(facesContext, id);
    if (component != null) {
      return jQuerySelector(facesContext, component);
    }

    return null;
  }

  /**
   * Utility method which finds component's jQuery selector based on component's clientId.
   */
  private static String jQuerySelector(FacesContext facesContext, UIComponent component) {
    if (facesContext == null) {
      throw new IllegalArgumentException("facesContext can't be null");
    }
    if (component == null) {
      throw new IllegalArgumentException("component can't be null");
    }
    String clientId = component.getClientId(facesContext);
    return "#" + escapeCSSMetachars(escapeCSSMetachars(clientId));
  }

  /**
   * <p>
   * Escapes CSS meta-characters in string according to <a href="http://api.jquery.com/category/selectors/">jQuery
   * selectors</a> document.
   * </p>
   *
   * @param s {@link String} to escape meta-characters in
   * @return string with escaped characters.
   */
  private static String escapeCSSMetachars(String s) {
    if (s == null || s.length() == 0) {
      return s;
    }

    StringBuilder builder = new StringBuilder();

    int start = 0;
    int idx = 0;

    int length = s.length();

    for (; idx < length; idx++) {
      char c = s.charAt(idx);

      int searchIdx = Arrays.binarySearch(CSS_SELECTOR_CHARS_TO_ESCAPE, c);
      if (searchIdx >= 0) {
        builder.append(s.substring(start, idx));

        builder.append("\\");
        builder.append(c);

        start = idx + 1;
      }
    }

    builder.append(s.substring(start, idx));

    return builder.toString();
  }

  private static char[] createSortedCharArray(String s) {
    char[] cs = s.toCharArray();
    Arrays.sort(cs);
    return cs;
  }
}
