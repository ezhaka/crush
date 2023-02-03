import * as React from 'react';
import {createContext, useCallback, useEffect, useState} from "react";
import {approvePermissionRequest, fetchSpaceUserToken, UserTokenData} from "../UserTokenData";
import {SendValentineForm} from './SendValentineForm';
import {httpGet} from "../api/http";
import {ValentineView} from "./ValentineView";
import "./App.css"
import {Button} from "./Button";

interface RootPage {
    kind: "root";
}

interface SendFormPage {
    kind: "sendForm";
}

interface ValentinePage {
    kind: "valentine";
    valentine: Valentine;
}

type Page = RootPage | SendFormPage | ValentinePage

export const PageContext = createContext<((page: Page) => void) | undefined>(undefined)

function App() {
    const onClick = useCallback(() => {
        const channel = new MessageChannel();
        window.parent.postMessage({
            type: "NavigateBackRequest",
        }, "*", [channel.port2]);
    }, [])

    const [token, setToken] = useState<UserTokenData>()

    useEffect(() => {
        const fetch = async () => {
            // await approvePermissionRequest()
            const token = await fetchSpaceUserToken()
            setToken(token)
        }

        fetch().catch(console.error)
    }, [])

    const [valentines, setValentines] = useState<Valentine[]>()

    useEffect(() => {
        const fetch = async () => {
            if (token) {
                const res = await httpGet(`/homepage/get-incoming-valentines`, token.token)
                const json = await res.json()
                setValentines(json.data)
            }
        }

        fetch().catch(console.error)
    }, [token])

    const [page, setPage] = useState<Page>({kind: "root"})

    return (
        <>
            <PageContext.Provider value={setPage}>
                <div className="page">
                    {/*<button onClick={onClick}>BACK</button>*/}

                    <div className="logo"></div>
                    <div className="promo-header">Make this Valentine's Day very special!</div>
                    <div className="promo-text">Our secure and confidential service ensures that your feelings will
                        reach your crush without revealing your identity. Choose from our selection of beautiful
                        valentines and add a secret message if you wish.
                    </div>

                    <Button title="Send secret valentine" action={() => {
                        setPage({kind: "sendForm"})
                    }}/>


                    {page.kind == "root" && <div>
                        {valentines?.map(v => <div key={v.id} onClick={() => {
                            setPage({kind: "valentine", valentine: v})
                        }}>❤️</div>)}

                        {/*<button onClick={() => setPage({kind: "sendForm"})}>SEND</button>*/}

                    </div>}

                    {page.kind == "sendForm" && token && <SendValentineForm token={token}/>}

                    {page.kind == "valentine" && <ValentineView valentine={page.valentine}/>}
                </div>
            </PageContext.Provider>
        </>
    );
}

export default App;