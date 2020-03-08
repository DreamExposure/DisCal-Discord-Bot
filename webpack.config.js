const path = require('path');

module.exports = {
	mode: "production",
	entry: './web/src/main/javascript/index.ts',
	module: {
		rules: [{
			test: /\.tsx?$/,
			use: 'ts-loader',
			exclude: /node_modules/,
		}],
	},
	resolve: {
		extensions: ['.ts', '.tsx', '.js'],
		alias: {
			'@': path.resolve('web/src/main/javascript')
		}
	},
	output: {
		path: path.resolve(__dirname, "web/src/main/resources/static/assets/js"),
		filename: 'bundle.js'
	},
};