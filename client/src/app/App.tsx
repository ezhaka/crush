import * as React from 'react';
import {createContext, useCallback, useEffect, useState} from "react";
import {fetchSpaceUserToken, UserTokenData} from "../UserTokenData";
import {SendValentineForm} from './SendValentineForm';
import {httpGet} from "../api/http";
import {ValentineViewPage} from "./ValentineViewPage";
import "./App.css"
import { RootPage } from './RootPage';

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

    const [valentines, setValentines] = useState<Valentine[] | undefined>(undefined)

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
                    {page.kind == "root" && <RootPage valentines={valentines} token={token} />}
                    {page.kind == "sendForm" && token && <SendValentineForm token={token}/>}
                    {page.kind == "valentine" && <ValentineViewPage valentine={page.valentine} token={token}/>}
                </div>
            </PageContext.Provider>
        </>
    );
}

export default App;