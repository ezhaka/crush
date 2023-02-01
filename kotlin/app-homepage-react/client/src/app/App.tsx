import './App.css';
import {AppTabs} from "./components/AppTabs";
import AsyncSelect from "react-select/async";
import * as utils from "./service/utils";
import {ChatChannel} from "./service/chatChannel";
import {Theme} from "react-select";
import * as theme from "./service/theme";
import {useState} from "react";

export const App = () => {

    return <>



        <div className="app">
            <span className="app-header">
                Demo application
            </span>
            <span className="app-description">
                Interaction between the app iframe, app server and Space
            </span>

            <AppTabs/>





        </div>
    </>
}
