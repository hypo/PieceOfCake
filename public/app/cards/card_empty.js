if (window.app === undefined) window.app = {};
if (window.app.cards == undefined) window.app.cards = {};

window.app.cards.EmptyCard = React.createClass({
  displayName: 'EmptyCard',
  render: function() {
    return (
      React.DOM.div({className: 'content'})
    );
  }
});
