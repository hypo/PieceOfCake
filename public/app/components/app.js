if (window.app === undefined) window.app = {};

window.app.App = React.createClass({
  displayName: 'App',
  render: function() {
    var children = [
      app.CardViewport({card: this.props.card})
    ];

    if (this.props.showSpinner)
      children.push(app.BlockerSpinner());

    if (this.props.showTitleBar)
      children.push(app.HeaderBar({title: this.props.title, showBackButton: this.props.showBackButton}));

    children.push(app.ActionStack({actions: this.props.actions}));

    if (this.props.flash_message !== undefined && this.props.flash_message != "") {
      children.push(
        React.addons.CSSTransitionGroup({transitionName: 'fade'},
          React.DOM.div({className: 'flash_message'},
            this.props.flash_message
          )
        )
      );
    } else {
      children.push(
          React.addons.CSSTransitionGroup({transitionName: 'fade', children: []})
      );
    }
    return (
      React.DOM.div(
        {children: children}
      )
    );
  }
});
