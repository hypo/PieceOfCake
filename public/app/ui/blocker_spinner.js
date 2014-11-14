if (window.app === undefined) window.app = {};

window.app.BlockerSpinner = React.createClass({
  displayName: 'BlockerSpinner',
  render: function() {
    return (
      React.DOM.div({className: "blocker"},
        React.DOM.svg(
          {
            className: "spinner",
            width: "65px",
            height: "65px",
            viewBox: "0 0 66 66",
            xmlns: "http://www.w3.org/2000/svg"
          },
          React.DOM.circle({
            className: "path",
            fill: "none",
            strokeWidth: "6",
            strokeLinecap: "round",
            cx: "33",
            cy: "33",
            r: "30"
          })
        )
      )
    );
  }
});

