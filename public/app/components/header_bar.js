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
          React.DOM.img({
            id: 'btn_back', style: backButtonStyle, onClick: this.handleBackButton,
            src: "/assets/img/left@2x.png",
            srcSet: "/assets/img/left.png 1x, /assets/img/left@2x.png 2x"
          })
        )
      )
    );
  }
});
