if (window.app === undefined) window.app = {};

window.app.HeaderBar = React.createClass({
  displayName: 'HeaderBar',
  handleBackButton: function() {
    app.CardStack.popCard();
  },
  render: function() {
    var backButtonStyle = {display: (this.props.showBackButton ? '' : 'none')};
    return (
      React.DOM.div({className: 'header', id: 'navbar'},
        React.DOM.div({className: 'container'},
          React.DOM.div({className: 'title'}, this.props.title),
          React.DOM.div({className: 'lsf', id: 'btn_back', style: backButtonStyle, onClick: this.handleBackButton}, 'left')
        )
      )
    );
  }
});
