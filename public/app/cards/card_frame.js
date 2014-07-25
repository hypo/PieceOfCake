if (window.app === undefined) window.app = {};
if (window.app.cards == undefined) window.app.cards = {};

window.app.cards.FrameCard = React.createClass({
  displayName: 'FrameCard',
  addQuantity: function() {
    var data = app.CardInfoStore.getData();
    app.CardInfoStore.update([['frame_qty', data.frame_qty + 1]]);
    app.CardStack.forceRender();
  },
  delQuantity: function() {
    var data = app.CardInfoStore.getData();
    if (data.frame_qty > 0) {
      app.CardInfoStore.update([['frame_qty', data.frame_qty - 1]]);
      app.CardStack.forceRender();
    }
  },
  render: function() {
    return (
      React.DOM.div({className: "content"},
        React.DOM.img({src: "/img/frame.png"}),
        React.DOM.div({className: "toolbar"},
          React.DOM.div({style: {float: "left"}},
            React.DOM.input({type: "text", value: this.props.frame_qty, className: "frame_qty", name: "frame_qty", disabled: "disabled"}),
            "份"
          ),
          React.DOM.div({style: {float: "right"}},
            React.DOM.button({className: "frame_qty", onClick: this.addQuantity}, "+"),
            React.DOM.button({className: "frame_qty", onClick: this.delQuantity}, "−")
          )
        )
      )
    );
  }
});
