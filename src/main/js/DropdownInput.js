'use strict';

const React = require('react')

class DropdownInput extends React.Component {

	constructor(props) {
		super(props)
		if (this.props.loadData) {
			this.props.loadData()
		}
	}

	render() {
		let options = this.props.data().map(option => <option key={option} value={option}>{option}</option>)

		return (<li className='control-group'>
			{this.props.settings.required.includes(this.props.name) && !this.props.settings[this.props.name] ?
				<label className='layout__item u-1/2-lap-and-up u-1/4-desk'>{this.props.label} <span className='required'>(*required)</span></label>
				:
				<label className='layout__item u-1/2-lap-and-up u-1/4-desk'>{this.props.label}</label>
			}
			<select className='layout__item u-1/2-lap-and-up u-3/4-desk'
					name={this.props.name}
					onChange={this.props.handleChange}>
				{options}
			</select>
		</li>)
	}

}

module.exports = DropdownInput