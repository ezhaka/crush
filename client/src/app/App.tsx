import * as React from 'react';
import {createContext, useCallback, useEffect, useRef, useState} from "react";
import {fetchSpaceUserToken, UserTokenData} from "../UserTokenData";
import {SendValentineForm} from './SendValentineForm';
import {httpGet} from "../api/http";
import {ValentineViewPage} from "./ValentineViewPage";
import "./App.css"
import {RootPage} from './RootPage';
import ReconnectingWebSocket from "reconnecting-websocket";

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
    const [page, setPage] = useState<Page>({kind: "root"})
    const [token, setToken] = useState<UserTokenData>()

    useEffect(() => {
        const fetch = async () => {
            const token = await fetchSpaceUserToken()
            setToken(token)
        }

        fetch().catch(console.error)
    }, [])

    const [valentines, setValentines] = useState<Valentine[] | undefined>(undefined)
    const [ws, setWs] = useState<ReconnectingWebSocket>()

    useEffect(() => {
        if (token) {
            const protocol = window.location.hostname === 'localhost' ? 'ws' : 'wss'
            setWs(new ReconnectingWebSocket(`${protocol}://${window.location.host}/api/websocket?token=${token.token}`))
        }
    }, [token])

    useEffect(() => {
        if (!ws) return

        const listener = (event: MessageEvent<any>) => {
            const message = JSON.parse(event.data)

            const type = message.type;
            const dotIndex = type.lastIndexOf('.')

            switch (type.substring(dotIndex + 1)) {
                case 'ValentineListInit': {
                    setValentines(message.data)
                    break
                }
                case 'ValentineReceived': {
                    setValentines([message.valentine, ...(valentines || [])])
                    break
                }
                case 'ValentineRead': {
                    setValentines(valentines.map(v => v.id === message.valentineId ? {...v, read: true} : v))
                    break
                }
            }
        }

        ws.addEventListener("message", listener)

        return () => {
            ws.removeEventListener("message", listener)
        }
    }, [ws, valentines, setValentines])

    return (
        <>
            <PageContext.Provider value={setPage}>
                <div className="page">
                    {page.kind == "root" && <RootPage valentines={valentines} token={token}/>}
                    {page.kind == "sendForm" && token && <SendValentineForm token={token}/>}
                    {page.kind == "valentine" && <ValentineViewPage valentine={page.valentine} token={token}/>}
                </div>
            </PageContext.Provider>
        </>
    );
}

export default App;