const path = require('path');
const MiniCssExtractPlugin = require('mini-css-extract-plugin')

module.exports = {
    mode: "production",
    entry: './web/src/main/javascript/index.ts',
    devtool: "inline-source-map",
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                use: 'ts-loader',
                exclude: /node_modules/,
            },
            {
                test: /\.css$/,
                use: [
                    {
                        loader: MiniCssExtractPlugin.loader
                    },
                    {
                        loader: 'css-loader', options: {
                            importLoaders: 1
                        }
                    }
                ]
            }
        ],
    },
    resolve: {
        extensions: ['.ts', '.tsx', '.js'],
        alias: {
            '@': path.resolve('web/src/main/javascript')
        }
    },
    output: {
        path: path.resolve(__dirname, "web/src/main/html/static/assets/js"),
        filename: 'bundle.js'
    },
    plugins: [
        new MiniCssExtractPlugin({
            filename: 'main.css'
        })
    ]
};
