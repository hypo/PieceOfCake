if (window.app === undefined) window.app = {};
if (window.app.cards == undefined) window.app.cards = {};

window.app.cards.DoneCard = React.createClass({
  displayName: 'DoneCard',
  render: function() {
    return (
      React.DOM.div({className: 'content card_done'},
        React.DOM.section({},
          _("YOUR_ORDER_NO")
        ),
        React.DOM.section({style:{"margin-top": "10px"}},
          React.DOM.span({className: 'order_id'}, "#" + this.props.order_id + _("ORDER_SUCCESSFUL"))
        ),
        React.DOM.section({style:{"margin-top": "10px"}},
          React.DOM.span({className: 'shipping_date'}, _("ESTIMATED_SHIPPING_DATE") + " " + this.props.estimated_shipping_date)
        ),
        React.DOM.section({className: "description"},
          React.DOM.br({}),
          _("TRACK_ORDER"),
          React.DOM.br({}),
          React.DOM.a({href: "http://hypo.cc/"}, "hypo.cc")
        )
      )
    );
  }
});
