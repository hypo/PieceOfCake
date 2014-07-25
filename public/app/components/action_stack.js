if (window.app === undefined) window.app = {};

window.app.ActionStack = React.createClass({
  displayName: 'ActionStack',
  render: function() {
    var i = 0;
    var actionButtons = (this.props.actions || []).map(function(action) {
      action['key'] = 'action_' + i;
      i += 1;
      return app.ActionButton(action);
    });

    return (
      React.DOM.div({id: 'actions'},
        React.DOM.div({className: 'container'}, actionButtons)
      )
    );
  }
});
