if (window.app === undefined) window.app = {};
if (window.app.cards == undefined) window.app.cards = {};

window.app.cards.DoneCard = React.createClass({
  displayName: 'DoneCard',
  render: function() {
    return (
      React.DOM.div({className: 'content card_done'},
        React.DOM.section({},
          "您的訂單編號"
        ),
        React.DOM.section({style:{"margin-top": "10px"}},
          React.DOM.span({className: 'order_id'}, "#" + this.props.order_id + " 訂購成功")
        ),
        React.DOM.section({style:{"margin-top": "10px"}},
          React.DOM.span({className: 'shipping_date'}, "預計出貨日 " + this.props.estimated_shipping_date)
        ),
        React.DOM.section({},
          React.DOM.br({}),
          "請利用以下連結查看訂單情形。",
          React.DOM.br({}),
          React.DOM.a({href: "http://hypo.cc/"}, "hypo.cc")
        )
      )
    );
  }
});
