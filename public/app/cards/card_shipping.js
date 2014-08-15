if (window.app === undefined) window.app = {};
if (window.app.cards == undefined) window.app.cards = {};

window.app.cards.ShippingCard = React.createClass({
  displayName: 'ShippingCard',
  render: function() {
    var country_id_mapping = {
      1: "Taiwan",
      3: "Hong Kong",
      5: "Macao",
      9: "Singapore",
      10: "Malaysia",
      11: "Japan",
      12: "Australia",
      13: "Netherlands",
      14: "USA",
      15: "UK",
      16: "Canada",
      17: "France",
      18: "South Korea",
      19: "Finland",
      20: "Spain",
      21: "South Africa",
      22: "Thailand",
      23: "China",
      24: "Switzerland",
      25: "Sweden",
      26: "Turkey",
      27: "New Zealand",
      28: "Germany",
      31: "PengHu",
      32: "JinMen",
      33: "Czech Republic",
      34: "Brazil",
      35: "Mazu",
      36: "Russia",
      37: "India",
      38: "Poland",
      39: "United Arab Emirates",
      40: "Greece",
      42: "Norway"
    };

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
              Object.keys(country_id_mapping).map(function(key) {
                var props = {};
                if (key == this.props.country) {
                  props['selected'] = 'selected';
                }
                props['value'] = key

                return React.DOM.option(props, country_id_mapping[key]);
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
