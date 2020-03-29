export const copyContentsToClipboard = element => {
    let didCopy = false;

    if (element) {
        element.contentEditable = 'true';
        const tempTextContents = element.innerText.split('\n')[0];
        element.innerText = element.innerText.replace(' ', '');
        const range = document.createRange();
        range.selectNodeContents(element);

        const selection = window.getSelection();

        if (selection) {
            selection.removeAllRanges();
            selection.addRange(range);

            didCopy = document.execCommand('copy');
            selection.removeAllRanges();
        }
        element.innerText = tempTextContents;
        element.contentEditable = 'false';
    }

    return didCopy;
};
