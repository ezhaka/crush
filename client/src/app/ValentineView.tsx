import * as React from 'react';
import {useContext} from "react";
import {PageContext} from "./App";

type Props = {
    valentine: Valentine
}

export const ValentineView = ({valentine}: Props) => {
    const setPage = useContext(PageContext)

    return (
        <div>
            {valentine.id}
            {valentine.message}
            <button onClick={() => setPage({kind:"root"})}>X</button>
        </div>
    )
}

