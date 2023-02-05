import * as React from "react";
import {useContext, useState} from "react";
import {UserTokenData} from "../UserTokenData";
import {httpPost} from "../api/http";
import "./SendValentineForm.css";
import {PageContext} from "./App";
import {ProfileSelectItem, ProfileSelector} from "./ProfileSelector";
import {Button} from "./Button";
import {valentineTypes} from "./ValentineType";
import {ValentineEditor} from "./ValentineEditor";

type Props = {
    token: UserTokenData;
}

export const SendValentineForm = ({token}: Props) => {
    const [profile, setProfile] = useState<ProfileSelectItem>()
    const [message, setMessage] = useState<string>()

    const submit = () => {
        // TODO: all to body!
        httpPost(`/homepage/send-valentine?receiverId=${profile?.id}&messageText=${message}`, token.token, {})
            .then(() => console.log("posted!"))
    }

    return (
        <>
            <div className="send-valentine-form">

                <ProfileSelector value={profile} onChange={setProfile} token={token}/>

                <ValentineEditor message={message} setMessage={setMessage} type={valentineTypes[0]} />

                <Button title="SEND IT!" action={submit}/>
            </div>
        </>
    )
}

