import * as React from "react";
import {useContext, useState} from "react";
import {UserTokenData} from "../UserTokenData";
import {httpPost} from "../api/http";
import "./Common.css"
import "./SendValentineForm.css";
import {ProfileSelectItem, ProfileSelector} from "./ProfileSelector";
import {Button} from "./Button";
import {valentineTypes} from "./ValentineType";
import {ValentineEditor} from "./ValentineEditor";
import {CloseableOverlay} from "./CloseableOverlay";
import {PageContext} from "./App";
import Carousel from "react-simply-carousel";

type Props = {
    token: UserTokenData;
}

export const SendValentineForm = ({token}: Props) => {
    const [profile, setProfile] = useState<ProfileSelectItem>()
    const [message, setMessage] = useState<string>('')
    const [posted, setPosted] = useState<boolean>(false)
    const [shakeEmpty, setShakeEmpty] = useState<boolean>(false)
    const [activeSlide, setActiveSlide] = useState(0);

    const submit = () => {
        if (!profile) {
            if (!shakeEmpty) {
                setShakeEmpty(true)
                setTimeout(() => setShakeEmpty(false), 1000)
            }
        } else {
            httpPost(`/api/send-valentine`, token.token, {
                receiverId: profile?.id,
                messageText: message,
                cardType: activeSlide
            })
                .then((response) => {
                    if (response.ok) {
                        setPosted(true)
                    } else {
                        // TODO: show error toast
                    }
                })
        }
    }

    let arrowStyles = {
        alignSelf: "center",
        background: "none",
        border: "none",
        padding: 0,
        cursor: "pointer",
    };

    return (
        <CloseableOverlay>
            {!posted && <div className="send-valentine-form">
                <div className={shakeEmpty && 'shake-empty-dropdown'}>
                    <ProfileSelector value={profile} onChange={setProfile} token={token}/>
                </div>

                <Carousel
                    containerProps={{
                        style: {
                            width: "100%",
                            maxWidth: "1280px",
                            justifyContent: "space-between",
                            userSelect: "text"
                        }
                    }}
                    activeSlideIndex={activeSlide}
                    activeSlideProps={{
                        style: {
                            background: "blue"
                        }
                    }}
                    onRequestChange={setActiveSlide}
                    forwardBtnProps={{
                        children: <div className="icon-arrow-right carousel-arrow"></div>,
                        style: arrowStyles
                    }}
                    backwardBtnProps={{
                        children: <div className="icon-arrow-left carousel-arrow"></div>,
                        style: arrowStyles
                    }}
                    itemsToShow={1}
                    speed={200}
                >
                    {valentineTypes.map((item, index) => (
                        <ValentineEditor key={index} message={message} setMessage={setMessage} type={item}/>
                    ))}
                </Carousel>

                <Button title="SEND IT!" action={submit}/>
            </div>}

            {posted && <ValentineIsSent/>}
        </CloseableOverlay>
    )
}

const kotikSrc = require(`./../../resources/kotik.png`)

const ValentineIsSent = () => {
    const setPage = useContext(PageContext)

    return (<div className="valentine-is-sent">
        <div className="logo"/>
        <div className="promo-header">Your Secret Valentine Has Been Sent!</div>
        <div className="promo-text">
            It will be delivered anonymously to your crush. We hope that this Valentine's Day will be filled with joy
            and surprises for both of you.
            <br/>
            <br/>
            Wishing you all the best on this day of love!
        </div>
        <Button title="OK!" action={() => {
            setPage({kind: "root"})
        }}/>
        <img src={kotikSrc} className="kotik"/>
    </div>)
}
