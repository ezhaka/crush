import * as React from 'react';
import {createContext, useCallback, useEffect, useLayoutEffect, useRef, useState} from "react";
import {fetchSpaceUserToken, UserTokenData} from "../UserTokenData";
import {SendValentineForm} from './SendValentineForm';
import {ValentineViewPage} from "./ValentineViewPage";
import "./App.css"
import {RootPage} from './RootPage';

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

    const markValentineAsRead = useCallback((valentine: Valentine) => {
        setValentines(valentines.map(v => {
            if (v.id === valentine.id) {
                return {...v, read: true}
            } else {
                return v
            }
        }))
    }, [valentines, setValentines])

    return (
        <>
            <PageContext.Provider value={setPage}>
                <div className="page">
                    {page.kind == "root" &&
                        <RootPage
                            valentines={valentines}
                            setValentines={setValentines}
                            token={token}
                        />}
                    {page.kind == "sendForm" && token && <SendValentineForm token={token}/>}
                    {page.kind == "valentine" &&
                        <ValentineViewPage
                            initialValentine={page.valentine}
                            valentines={valentines}
                            token={token}
                            markValentineAsRead={markValentineAsRead}
                        />}
                </div>
            </PageContext.Provider>
        </>
    );
}

export default App;