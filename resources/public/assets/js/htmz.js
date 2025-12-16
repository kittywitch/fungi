function htmz(frame) {
    // ---------------------------------8<-----------------------------------
    // Repeat GETs
    // ----------------------------------------------------------------------
    // This extension clears the iframe URL for a fresh start on next load
    if (frame.contentWindow.location.href === "about:blank") return;
    // --------------------------------->8-----------------------------------
    setTimeout(() => {
        document
            .querySelector(frame.contentWindow.location.hash || null)
        ?.replaceWith(...frame.contentDocument.body.childNodes);
        // ---------------------------------8<-----------------------------------
        frame.contentWindow.location.replace("about:blank");
        // ---------------------------------i>8-----------------------------------
    });
}

/*
 * HTMZ, despite being a micro-framework is quite anemic for the potential cases of
 * a user not actually using JavaScript, which I think is funny given that it talks
 * about Vanilla JS :p
 *
 * Intents:
 * - progressive enhancement!
 * - a way to detect htmz being used via ?htmz= query!
 */

function htmz_appender(link) {
    var url = new URL(link);
    url.searchParams.append("htmz", "");
    return url
}

function htmz_attr(element, attr) {
    element[attr] = htmz_appender(element[attr]);
}

window.onload = () => {
    var htmzs = document.getElementsByClassName("htmz");
    for (const element of htmzs) {
        if (element.hasAttribute("href")) {
            htmz_attr(element, "href");
        }
        if (element.hasAttribute("action")) {
            htmz_attr(element, "action");
        }
        if (element.hasAttribute("formaction")) {
            htmz_attr(element, "formaction");
        }
        element.setAttribute("target", "htmz");
    }
}
