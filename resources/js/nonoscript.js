function nonoscriptImplementer() {
  var nonoscript = document.getElementsByClassName("nonoscript");
  console.log(nonoscript);
  for (let item of nonoscript) {
    item.classList.remove("nonoscript");
  }
}
window.onload = nonoscriptImplementer();
