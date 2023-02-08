const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
    mode: 'development',
    entry: {
        app: './src/app/Main.tsx',
        notification: './src/notification/Main.tsx',
    },
    devtool: 'inline-source-map',
    output: {
        path: path.join(__dirname, '/dist/static'),
    },
    devServer: {
        static: './dist',
        port: 3001,
        proxy: [
            {
                context: ['/api', '/homepage', '/health'],
                target: 'http://localhost:8080',
                ws: true
            },
        ],
    },
    module: {
        rules: [
            {
                test: /\.jsx?$/,
                exclude: /node_modules/,
                loader: 'babel-loader'
            },
            {
                test: /\.tsx?$/,
                use: 'ts-loader',
                exclude: /node_modules/,
            },
            {
                test: /\.css$/i,
                use: ["style-loader", "css-loader"],
                exclude: /node_modules/,
            },
            {
                test: /\.png/,
                type: 'asset/resource'
            }
        ]
    },
    resolve: {
        extensions: ['.tsx', '.ts', '.js', '.png'],
    },
    plugins:[
        new HtmlWebpackPlugin({
            template: './src/app/index.html',
            chunks: ["app"],
        }),
        new HtmlWebpackPlugin({
            template: './src/notification/index.html',
            filename: "notification.html",
            chunks: ["notification"],
        })
    ]
}