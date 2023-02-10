import * as React from 'react';
import * as ReactDOM from 'react-dom';
import App from './App';
import {valentineTypes} from "./ValentineType";

function createPreloadLink(href: string, as: string = "image") {
    const link = document.createElement("link")
    link.rel = "prefetch"
    link.as = as
    link.href = href
    document.head.appendChild(link)
}

valentineTypes.forEach(type => {
    createPreloadLink(require(`./../../resources/valentines/${type.name}-fullscreen.png`))
    createPreloadLink(require(`./../../resources/valentines/${type.name}-sticker.png`))
    createPreloadLink(require(`./../../resources/valentines/${type.name}-sticker-new.png`))
})

createPreloadLink(require(`./../../resources/kotik.png`))
createPreloadLink(require(`./../../resources/new.svg`))
createPreloadLink(require(`./../../resources/font/font/fontello.woff2`), "font")

ReactDOM.render(<App />, document.getElementById('root'));