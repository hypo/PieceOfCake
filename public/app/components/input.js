if (window.app === undefined) window.app = {};

window.app.Input = React.createClass({
  displayName: 'Input',
  getDefaultProps: function() {
    return {
      type: 'text'
    };
  },
  render: function() {
    var placeholder = this.props.placeholder || "";
    if (this.props.label)
      placeholder = this.props.label + " " + placeholder;

    return (
      React.DOM.div({className: 'field'},
        React.DOM.input({
          type: this.props.type,
          name: this.props.name,
          placeholder: placeholder,
          defaultValue: this.props.defaultValue,
          pattern: this.props.pattern
        })
      )
    );
  }
});
