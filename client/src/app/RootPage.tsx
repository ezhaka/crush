import * as React from 'react';
import {Button} from "./Button";
import {useContext} from "react";
import {PageContext} from "./App";
import "./RootPage.css"

export const RootPage = () => {
    const setPage = useContext(PageContext)

    return (
        <div className="root-page">
            {/*<button onClick={onClick}>BACK</button>*/}

            {/*{valentines?.map(v => <div key={v.id} onClick={() => {*/}
            {/*    setPage({kind: "valentine", valentine: v})*/}
            {/*}}>❤️</div>)}*/}

            <div className="logo"></div>
            <div className="promo-header">Make this Valentine's Day very special!</div>
            <div className="promo-text">Our secure and confidential service ensures that your feelings will
                reach your crush without revealing your identity. Choose from our selection of beautiful
                valentines and add a secret message if you wish.
            </div>

            <Button title="Send secret valentine" action={() => {
                setPage({kind: "sendForm"})
            }}/>
        </div>

    )
}