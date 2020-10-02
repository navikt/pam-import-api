const HtmlWebPackPlugin = require('html-webpack-plugin');
const webpack = require('webpack');

module.exports = {
    devServer: {
        historyApiFallback: true
    },
    output: {
        filename: 'static/main.js'
    },
    module: {
        rules: [
            {
                test: /\.(js|jsx)$/,
                exclude: /node_modules/,
                use: {
                    loader: 'babel-loader'
                }
            },
            {
                test: /\.html$/,
                use: [
                    {
                        loader: 'html-loader'
                    }
                ]
            },
            {
                test: /\.(less|css)$/,
                use: [
                    {
                        loader: 'style-loader' // creates style nodes from JS strings
                    },
                    {
                        loader: 'css-loader' // translates CSS into CommonJS
                    },
                    {
                        loader: 'less-loader' // compiles Less to CSS
                    }
                ]
            }
        ]
    },
    plugins: [
        new webpack.DefinePlugin({
            __API__: "'http://localhost:9028/stillingsimport/api/v1/preview'"
        }),
        new HtmlWebPackPlugin({
            template: './src/index.html',
            filename: './index.html'
        })
    ]
};
