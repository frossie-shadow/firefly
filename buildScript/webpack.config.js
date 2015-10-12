import webpack from 'webpack';
import HtmlWebpackPlugin from 'html-webpack-plugin';
import ExtractTextPlugin from 'extract-text-webpack-plugin';
import path from 'path';
import fs from 'fs';


/* global config:true */

var exclude_dirs = [/node_modules/, /java/, /python/, /config/, /test/];
config.firefly_dir = config.firefly_dir || config.src;
config.project = config.project || path.resolve(config.src, '../../');

var def_config = {
    env         : process.env.NODE_ENV || "development",
    dist        : process.env.WP_BUILD_DIR || path.resolve(config.project, 'build/gwt', config.name),
    do_lint     : process.env.DO_LINT || false,
    index_html  : 'index.html',
    html_dir    : 'html',
    filename    : '[name].[hash].js',
    deploy_dir  : (process.env.HYDRA_ROOT || "/hydra") + `/server/tomcat/webapps/${config.name}`,
    alias       : {
            'ipac-firefly' : path.resolve(config.firefly_dir, 'js'),
            firefly : path.resolve(config.firefly_dir, 'js'),
            styles : path.resolve(config.src, 'styles')
        }
}

config.alias = Object.assign(def_config.alias, config.alias);
config = Object.assign(def_config, config);


const globals = {
            'process.env'  : {
                'NODE_ENV' : JSON.stringify(config.env)
            },
            'NODE_ENV'     : config.env,
            '__DEV__'      : config.env === 'local' || config.env === 'dev',
            '__PROD__'     : config.env === 'ops' || config.env === 'test',
            '__DEBUG__'    : config.env === 'development' && process.env.DEBUG
        };

var output_path = config.dist;
if (globals.__DEBUG__) {
    output_path = config.deploy_dir;
}


/*
* creating the webpackConfig based on the project's config for webpack to work on.
*
*/
var webpackConfig = {
    name    : config.name,
    target  : 'web',
    devtool : 'source-map',
    entry   : {
        app : [
            config.entry
        ]
    },
    output : {
        filename   : config.filename,
        path       : output_path
    },
    plugins : [
        new webpack.DefinePlugin(Object.assign(globals, {
            __CLIENT__ : true,
            __SCRIPT_NAME__ : "'"+ config.filename + "'"
        })),
        new webpack.optimize.OccurrenceOrderPlugin(),
        new webpack.optimize.DedupePlugin(),
        new ExtractTextPlugin(`${config.name}.css`),

    ],
    resolve : {
        extensions : ['', '.js', '.jsx'],
        alias : config.alias
    },
    module : {
        loaders : [
            {
                test : /\.(js|jsx)$/,
                exclude: exclude_dirs,
                loaders : ['babel-loader']
            },
            {
                test    : /\.css$/,
                exclude: exclude_dirs,
                loaders : [
                    'style-loader',
                    'css-loader',
                    'autoprefixer?browsers=last 2 version',
                    'sass-loader?includePaths[]=' + config.alias.styles
                ]
            }
        ]
    },
    eslint : {
        configFile  : path.resolve(config.project,'.eslintrc'),
        failOnError : false,
        emitWarning : false
    }
};

// ----------------------------------
// Vendor Bundle Configuration
// ----------------------------------
//webpackConfig.entry.vendor = [
//    'history',
//    'immutable',
//    'react',
//    'react-redux',
//    'react-router',
//    'redux',
//    'redux-devtools',
//    'redux-devtools/lib/react'
//];
//
//// NOTE: this is a temporary workaround. I don't know how to get Karma
//// to include the vendor bundle that webpack creates, so to get around that
//// we remove the bundle splitting when webpack is used with Karma.
//const commonChunkPlugin = new webpack.optimize.CommonsChunkPlugin(
//    'vendor', '[name].[hash].js'
//);
//commonChunkPlugin.__KARMA_IGNORE__ = true;
//webpackConfig.plugins.push(commonChunkPlugin);

// ----------------------------------
// Environment-Specific Defaults
// ----------------------------------

if (globals.__PROD__) {

    // commented out for now.. may want to use it later on.
    // Compile CSS to its own file in production.
    //webpackConfig.module.loaders = webpackConfig.module.loaders.map(loader => {
    //    if (/css/.test(loader.test)) {
    //        const [first, ...rest] = loader.loaders;
    //
    //        loader.loader = ExtractTextPlugin.extract(first, rest.join('!'));
    //        delete loader.loaders;
    //    }
    //
    //    return loader;
    //});

    webpackConfig.plugins.push(
        new webpack.optimize.UglifyJsPlugin({
            compress : {
                warnings  : false,
                unused    : true,
                dead_code : true
            }
        })
    );
}

// ------------------------------------
// Optional Configuration
// ------------------------------------

// if index_html exists, insert script tag to load built javascript bundles(s).
if (fs.existsSync(path.resolve(config.src, config.html_dir, config.index_html))) {
    webpackConfig.plugins.push(
        new HtmlWebpackPlugin({
            template : path.resolve(config.src, config.html_dir, config.index_html),
            hash     : false,
            filename : config.index_html,
            minify   : globals.__PROD__,
            inject   : 'body'
        })
    );
}

if (config.do_lint) {
    webpackConfig.module.preLoaders = [
        {
            test : /\.(js|jsx)$/,
            exclude: exclude_dirs,
            loaders : ['eslint-loader']
        }
    ];
}

export default webpackConfig;


//console.log ("--------------- CONFIG --------------");
//console.log (JSON.stringify(config, null,2));
//console.log ("--------------- WEBPACK_CONFIG --------------");
//console.log (JSON.stringify(webpackConfig, null,2));