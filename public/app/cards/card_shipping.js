if (window.app === undefined) window.app = {};
if (window.app.cards == undefined) window.app.cards = {};

window.app.cards.ShippingCard = React.createClass({
  displayName: 'ShippingCard',
  render: function() {
    return (
      React.DOM.div({className: 'content'},
        React.DOM.section({},
          React.DOM.div({className: 'field'},
            React.DOM.input({type: 'text', name: 'name', placeholder: '收件人姓名', defaultValue: this.props.name})
          )
        ),
        React.DOM.section({},
          React.DOM.div({className: 'field'},
            React.DOM.input({type: 'text', name: 'tel', placeholder: '收件人電話', defaultValue: this.props.tel})
          )
        ),
        React.DOM.section({},
          React.DOM.div({className: 'field'},
            React.DOM.select({name: 'country'},
              Object.keys(app.countries).map(function(key) {
                var props = {};
                if (key == this.props.country) {
                  props['selected'] = 'selected';
                }
                props['value'] = key

                return React.DOM.option(props, app.countries[key]);
              }.bind(this))
            )
          )
        ),
        React.DOM.section({},
          React.DOM.div({className: 'field'},
            React.DOM.input({type: 'text', name: 'city', placeholder: '城市', defaultValue: this.props.city})
          )
        ),
        React.DOM.section({},
          React.DOM.div({className: 'field half'},
            React.DOM.input({type: 'text', className: 'half', name: 'area', placeholder: '區', defaultValue: this.props.area})
          ),
          React.DOM.div({className: 'field half'},
            React.DOM.input({type: 'number', className: 'half', name: 'zipcode', placeholder: '郵遞區號', defaultValue: this.props.zipcode})
          )
        ),
        React.DOM.section({},
          React.DOM.div({className: 'field'},
            React.DOM.textarea({name: 'addr', rows: 5, placeholder: '地址', defaultValue: this.props.addr})
          )
        )
      )
    );
  }
});
