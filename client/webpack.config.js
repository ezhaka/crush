const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = (env, argv) => {
    return ({
        entry: {
            app: './src/app/Main.tsx',
        },
        devtool: 'inline-source-map',
        output: {
            filename: '[name].[hash].js',
            path: path.join(__dirname, '/dist/static'),
            publicPath: argv.mode === 'development' ? '/' : '/static/'
        },
        devServer: {
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
                    test: /\.(png|svg|eot|ttf|woff|woff2)$/i,
                    type: 'asset/resource'
                }
            ]
        },
        resolve: {
            extensions: ['.tsx', '.ts', '.js', '.png'],
        },
        plugins: [
            new HtmlWebpackPlugin({
                template: './src/app/index.html',
                chunks: ["app"],
                scriptLoading: 'blocking'
            }),
        ]
    });
}