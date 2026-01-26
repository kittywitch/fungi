function theme_css(name) {
  return "/assets/css/arborium/" + name + ".css";
}

function theme_restore() {
  var ls_dark = localStorage.getItem("arborium-theme-dark");
  var ls_light = localStorage.getItem("arborium-theme-light");
  if (ls_dark == null) {
    ls_dark = "catppuccin-macchiato";
    localStorage.setItem("arborium-theme-dark", ls_dark);
  }
  if (ls_light == null) {
    ls_light = "catppuccin-latte";
    localStorage.setItem("arborium-theme-light", ls_light);
  }
  var arb_dark = document.getElementById("arborium-theme-dark");
  var arb_light = document.getElementById("arborium-theme-light");
  var syntax_highlighting_dark = document.getElementById("syntax-highlighting-dark");
  var syntax_highlighting_light = document.getElementById("syntax-highlighting-light");
  arb_dark.setAttribute("href", theme_css(ls_dark));
  arb_light.setAttribute("href", theme_css(ls_light));
  syntax_highlighting_dark.setAttribute("value", ls_dark);
  syntax_highlighting_light.setAttribute("value", ls_light);
  var options = syntax_highlighting_dark.options;
  var n = options.length;
  for (var i = 0; i<n; i++) {
    if (options[i].value == ls_dark) {
      options[i].selected = true;
    }
  }
  var options = syntax_highlighting_light.options;
  var n = options.length;
  for (var i = 0; i<n; i++) {
    if (options[i].value == ls_light) {
      options[i].selected = true;
    }
  }
}

var radios = document.getElementsByName("color-scheme");
var prev = localStorage.getItem("color-scheme");

document.documentElement.setAttribute("data-theme", prev);
for (var i = 0; i < radios.length; i++) {
  var _this = radios[i];
  _this.checked = _this.value == prev;
  radios[i].addEventListener("change", function() {
    if (this.value !== prev) {
      prev = this.value;
      localStorage.setItem("color-scheme", this.value);
      document.documentElement.setAttribute("data-theme", this.value);
    }
  });
}
var button = document.getElementById("clear-color-scheme");
button.addEventListener("click", function() {
  for (var i = 0; i < radios.length; i++) {
    var _this = radios[i];
    _this.checked = false;
    localStorage.removeItem("color-scheme");
    document.documentElement.setAttribute("data-theme", "");
  }
});

var ls_dark = localStorage.getItem("arborium-theme-dark");
var ls_light = localStorage.getItem("arborium-theme-light");
var syntax_highlighting_dark = document.getElementById("syntax-highlighting-dark");
var syntax_highlighting_light = document.getElementById("syntax-highlighting-light");
syntax_highlighting_dark.addEventListener("change", (event) => {
  if (event.target.value !== ls_dark) {
    localStorage.setItem("arborium-theme-dark", event.target.value);
  }
  var theme = theme_css(event.target.value);
  var arb = document.getElementById("arborium-theme-dark");
  arb.setAttribute("href", theme);
});
syntax_highlighting_light.addEventListener("change", (event) => {
  if (event.target.value !== ls_light) {
    localStorage.setItem("arborium-theme-light", event.target.value);
  }
  var theme = theme_css(event.target.value);
  var arb = document.getElementById("arborium-theme-light");
  arb.setAttribute("href", theme);
});

theme_restore();
