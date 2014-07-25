if (window.app === undefined) window.app = {};

var CardStack = function() {
  this.stack = [];
}

CardStack.prototype.pushCard = function(card_name) {
  var old_card_name = this.stack[this.stack.length - 1];
  if (this.stack.length > 0)
    history.replaceState({card_stack: this.stack, card_info: app.CardInfoStore.getData()}, app.CardsStore.getCard(old_card_name).title, "#" + old_card_name);
  else
    history.replaceState({card_stack: [card_name], card_info: app.CardInfoStore.getData()}, app.CardsStore.getCard(old_card_name).title, "#" + old_card_name);

  this.stack.push(card_name);
  this.forceRender();

  history.pushState({
    card_stack: this.stack,
    card_info: app.CardInfoStore.getData(),
    scroll_top: document.body.scrollTop
  }, app.CardsStore.getCard(card_name).title, "#" + card_name);
  document.body.scrollTop = 0;

  setTimeout(function() {
    var actionHeight = document.querySelector('#actions').offsetHeight;
    if (actionHeight > 0)
      document.querySelector('#card-viewport .content').style.paddingBottom = (actionHeight + 10) + 'px';
    else
      document.querySelector('#card-viewport .content').style.paddingBottom = '0';
  }, 10);
}

CardStack.prototype.popCard = function() {
  if (this.stack.length == 1) {
    console.log("[WARN] CardStack: unable to pop card, you're already at root level.")
    return;
  }

  this.stack.pop();
  app.reverseAnimation = true;
  this.forceRender(function() {
    app.reverseAnimation = false;
  });
}

CardStack.prototype.flashError = function(error) {
  app.appInstance.setProps({flash_message: error});
  setTimeout(function() { app.appInstance.setProps({flash_message: ""}); }, 3000);
}

CardStack.prototype.forceRender = function(callback) {
  if (callback === undefined)
    window.app.appInstance = React.renderComponent(app.App(this._preparePropsUpdate()), document.body);
  else
    window.app.appInstance = React.renderComponent(app.App(this._preparePropsUpdate()), document.body, callback);
}

CardStack.prototype._preparePropsUpdate = function() {
  var obj = {};
  var card = app.CardsStore.getCard(this.stack[this.stack.length - 1]);
  var data = app.CardInfoStore.getData();
  data['key'] = card.name;

  obj['card'] = card.content(data);
  obj['actions'] = card.actions;
  obj['title'] = card.title;
  obj['showBackButton'] = card.showBackButton === undefined ? this.stack.length > 1 : card.showBackButton;
  obj['showTitleBar'] = card.noTitleBar ? false : true

  return obj;
}

window.app.CardStack = new CardStack();
window.onpopstate = function(e) {
  if (e.state === null) return;

  app.CardStack.stack = e.state.card_stack;
  app.CardInfoStore.setData(e.state.card_info);
  document.body.scrollTop = e.state.scroll_top;
  app.CardStack.forceRender();
}
