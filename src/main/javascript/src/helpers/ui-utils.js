import { alert as alertStore } from "@/store/alert.module";
import { configuration as configurationStore } from "@/store/configuration.module";

export function widenContainer(from = "col-md-6", to = "col-md-10") {
    const element = document.getElementsByClassName("steps-container-col")[0];
    if (!element) {
        return;
    }
    element.classList.remove(from);
    element.classList.add(to);
}

export function shrinkContainer(from = "col-md-10", to = "col-md-6") {
    const element = document.getElementsByClassName("steps-container-col")[0];
    if (!element) {
        return;
    }
    element.classList.remove(from);
    element.classList.add(to);
}

export function adjustBodyTopPadding(to = "pt-4", from = "pt-4") {
    const element = document.getElementsByClassName("experiment-steps__body")[0];
    if (!element) {
        return;
    }
    element.classList.remove(from);
    if (to) {
        element.classList.add(to);
    }
}

export function getColor(property) {
    // getComputedStyle, not documentElement.style - the latter only reads the element's own
    // inline style attribute, never a CSS custom property defined via a stylesheet rule (e.g.
    // the :root {} block in variables.scss, which is how every one of these is actually
    // defined) - it would silently return "" for any of them. getComputedStyle resolves the
    // full cascade, inline style included, so this covers both cases.
    return getComputedStyle(document.documentElement).getPropertyValue(property).trim();
}

export function deleteAttributesFromObservedElement(parentClass, nodeClass, elementClass, attributes) {
  // remove attributes from elements for
  const observerForAddedElement = new MutationObserver(function(mutationsList) {
    for (const mutation of mutationsList) {
      if (mutation.type === "childList" && mutation.addedNodes.length > 0) {
        for (const node of mutation.addedNodes) {
          if (node.nodeType === 1 && node.classList.contains(nodeClass)) {
            let elements = node.querySelectorAll(elementClass);
            elements = elements.length === 0 ? [node] : elements;
            elements.forEach((el) => {
              attributes.forEach((attribute) => {
                el.removeAttribute(attribute);
              });
            });
          }
        }
      }
    }
  });
  const parentElement = document.querySelectorAll(parentClass);
  if (parentElement.length > 0) {
    observerForAddedElement.observe(parentElement[0], { childList: true, subtree: true });
  } else {
    console.warn(`Parent element with class ${parentClass} not found. MutationObserver not set.`);
  }
}

export function addAttributesToObservedElement(parentClass, nodeClass, elementClass, attributes) {
  // add attributes to elements for
  const observerForAddedElement = new MutationObserver(function(mutationsList) {
    for (const mutation of mutationsList) {
      if (mutation.type === "childList" && mutation.addedNodes.length > 0) {
        for (const node of mutation.addedNodes) {
          if (node.nodeType === 1 && node.classList.contains(nodeClass)) {
            let elements = node.querySelectorAll(elementClass);
            elements = elements.length === 0 ? [node] : elements;
            elements.forEach((el) => {
              attributes.forEach((attribute) => {
                el.setAttribute(attribute.name, attribute.value);
              });
            });
          }
        }
      }
    }
  });
  const parentElement = document.querySelectorAll(parentClass);
  if (parentElement.length > 0) {
    observerForAddedElement.observe(parentElement[0], { childList: true, subtree: true });
  } else {
    console.warn(`Parent element with class ${parentClass} not found. MutationObserver not set.`);
  }
}

export function deleteAttributesFromElement(elementClass, attributes) {
  const elements = document.querySelectorAll(elementClass);
  elements.forEach((el) => {
    attributes.forEach((attribute) => {
      el.removeAttribute(attribute);
    });
  });
}

export function addAttributesToElement(elementClass, attributes) {
  const elements = document.querySelectorAll(elementClass);
  elements.forEach((el) => {
    attributes.forEach((attribute) => {
      el.setAttribute(attribute.name, attribute.value);
    });
  });
}

export function getAttributeFromElement(elementClass, attribute) {
  const element = document.querySelectorAll(elementClass);

  return element.length > 0 ? element[0].getAttribute(attribute) : null;
}

export function handleTooltipOpening(tooltipRef) {
  // close all other tooltips except the one passed in
  Object.keys(this.$refs)
    .filter(ref => ref !== tooltipRef)
    .forEach(ref => {
      if (this.$refs[ref] && this.$refs[ref][0]) {
        this.$refs[ref][0].close();
      }
    });
}

export function statusAlert(type, message) {
  return {
    alertType: type,
    alertMessage: message
  }
}

export function createStatusAlert(statusAlert) {
  const aStore = alertStore();
  const actionName = statusAlert.alertType || aStore.statuses.info;
  aStore[actionName](statusAlert.alertMessage);
}

export function showSkipLink(show) {
  configurationStore().update({
    name: "showSkipLink",
    value: show
  });
}

// Vuetify's own v-menu "location" prop is supposed to auto-flip when there's not
// enough room in the preferred direction, but that didn't hold up in practice (a
// menu opening "top start" near the top of a scrolled page rendered clipped behind
// the browser's own chrome instead of flipping to open below - confirmed on a real
// page, not just a synthetic reproduction). Measuring available space ourselves on
// pointerdown (fires before the click v-menu's own activator listens for, so this
// runs first regardless of Vue's event-listener merge order for two listeners on the
// same "click" event) and picking the location explicitly sidesteps trusting that
// heuristic at all. estimatedMenuHeight only needs to be a reasonable upper bound for
// the specific menu calling this (its real content isn't in the DOM to measure until
// it's open) - a bit too generous just means it flips to "bottom" a little more
// readily than strictly necessary, which is harmless, unlike a clipped menu.
export function pickMenuLocation(buttonEl, estimatedMenuHeight) {
  if (!buttonEl) {
    return "top start";
  }

  const rect = buttonEl.getBoundingClientRect();
  const spaceAbove = rect.top;
  const spaceBelow = window.innerHeight - rect.bottom;

  return spaceAbove < estimatedMenuHeight && spaceBelow > spaceAbove
    ? "bottom start"
    : "top start";
}

// belt-and-suspenders on top of pickMenuLocation (which only predicts a location
// before the menu opens): confirmed on a real deployed page that a v-menu can still
// render clipped above the viewport even with that pre-emptive pick in place - rather
// than chase exactly why the prediction didn't hold, this checks the ACTUAL rendered
// position once the menu is open, which can't be wrong for the same reason the
// prediction was. v-menu's activator gets aria-controls pointing at the teleported
// overlay's id (Vuetify sets both from the same internal id, regardless of how many
// other menu instances exist elsewhere on the page - e.g. one per row in a table),
// so this doesn't need a per-instance Vue template ref to find the right one; a plain
// DOM lookup from the button that was actually clicked is enough.
export function isMenuOverlayClippedAbove(buttonEl) {
  const overlayId = buttonEl?.getAttribute("aria-controls");
  const overlayEl = overlayId ? document.getElementById(overlayId) : null;

  if (!overlayEl) {
    return false;
  }

  return overlayEl.getBoundingClientRect().top < 0;
}
