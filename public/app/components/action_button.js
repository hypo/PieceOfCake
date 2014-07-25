if (window.app === undefined) window.app = {};

window.app.ActionButton = React.createClass({
  displayName: 'ActionButton',
  handleClick: function(e) {
    app.handleAction(this.props);
  },
  render: function() {
    return (
      React.DOM.div({className: 'action', onClick: this.handleClick}, this.props.text)
    );
  }
});
