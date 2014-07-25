if (window.app === undefined) window.app = {};

window.app.CardViewport = React.createClass({
  displayName: 'CardViewport',
  render: function() {
    return (
      React.DOM.div({className: 'container'},
        React.DOM.div({id: 'card-viewport'},
          ext.CSSTransitionGroup({transitionName: 'slide', children: [this.props.card]})
        )
      )
    );
  }
});
