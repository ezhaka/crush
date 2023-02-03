import * as React from "react";
import {UserTokenData} from "../UserTokenData";
import {useContext, useState} from "react";
import {httpGet, httpPost} from "../api/http";
import "./SendValentineForm.css";
import {PageContext} from "./App";
import {ProfileSelectItem, ProfileSelector} from "./ProfileSelector";
import {Button} from "./Button";

type Props = {
    token: UserTokenData;
}

export const SendValentineForm = ({token}: Props) => {
    const [profile, setProfile] = useState<ProfileSelectItem>()
    const [message, setMessage] = useState<string>()
    const setPage = useContext(PageContext)

    const submit = () => {
        // TODO: all to body!
        httpPost(`/homepage/send-valentine?receiverId=${profile?.id}&messageText=${message}`, token.token, {})
            .then(() => console.log("posted!"))
    }

    return (
        <>
            <div className="send-valentine-form">
                <ProfileSelector value={profile} onChange={setProfile} token={token}/>

                <textarea value={message} onChange={(e) => setMessage(e.target.value)} rows={5}/>

                <Button title="SEND IT!" action={submit}/>
                <button onClick={() => setPage({kind: "root"})}>X</button>
            </div>
        </>
    )
}