if (window.app === undefined) window.app = {};

window.app.Input = React.createClass({
  displayName: 'Input',
  getDefaultProps: function() {
    return {
      type: 'text'
    };
  },
  render: function() {
    return (
      React.DOM.div({className: 'field'},
        React.DOM.table({},
          React.DOM.tr({},
            React.DOM.td({}, this.props.label),
            React.DOM.td({}, React.DOM.input({
              type: this.props.type,
              name: this.props.name,
              placeholder: this.props.placeholder,
              defaultValue: this.props.defaultValue,
              pattern: this.props.pattern
            }))
          )
        )
      )
    );
  }
});
