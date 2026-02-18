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
