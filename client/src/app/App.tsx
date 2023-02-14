import * as React from 'react';
import {createContext, useCallback, useEffect, useLayoutEffect, useRef, useState} from "react";
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

    useLayoutEffect(() => {
        window.scroll(0, 0)
    }, [page])

    useEffect(() => {
        setInterval(() => {
            fetchSpaceUserToken().then(t => setToken(t)).catch(console.error)
        }, 1000 * 60 * 4)

        fetchSpaceUserToken().then(t => setToken(t)).catch(console.error)
    }, [])

    const [valentines, setValentines] = useState<Valentine[] | undefined>(undefined)
    const [ws, setWs] = useState<ReconnectingWebSocket>()

    useEffect(() => {
        let webSocket: ReconnectingWebSocket | undefined

        if (token) {
            const protocol = window.location.hostname === 'localhost' ? 'ws' : 'wss'
            webSocket = new ReconnectingWebSocket(`${protocol}://${window.location.host}/api/websocket?token=${token.token}`);
            setWs(webSocket)
        }

        return () => {
            webSocket?.close()
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
                    {page.kind == "valentine" &&
                        <ValentineViewPage initialValentine={page.valentine} valentines={valentines} token={token}/>}
                </div>
            </PageContext.Provider>
        </>
    );
}

export default App;