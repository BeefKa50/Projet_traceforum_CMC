function createBarChart(labels,data){
    const div = document.createElement('div');
    const ctx = document.createElement("canvas")
    ctx.setAttribute("width","100")
    ctx.setAttribute("height","40")

    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Average reading time',
                data: data,
                borderWidth: 1
            }]
        },
        options: {
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });

    ctx.setAttribute("width","100")
    ctx.setAttribute("height","40")
    div.appendChild(ctx)
    return div
}

function createPieChart(labels,data){

    const div = document.createElement('div');
    const ctx = document.createElement("canvas")
    ctx.setAttribute("width","100")
    ctx.setAttribute("height","40")

    new Chart(ctx, {
        type: 'pie',
        data: {
          labels: labels,
          datasets: [{
            label: "Messages read",
            backgroundColor: ["#3e95cd", "#8e5ea2"],
            data: data
          }]
        },
        options: {
          title: {
            display: true,
            text: 'Messages read completely and partially'
          }
        }
    });

    ctx.setAttribute("width","100")
    ctx.setAttribute("height","40")
    div.appendChild(ctx)
    return div
}

window.addEventListener("DOMContentLoaded", (event) => {
    firstUser = "tdelille"
    const xhr = new XMLHttpRequest();
    xhr.open("POST", "/indicators/" + firstUser, true);
    xhr.getResponseHeader("Content-type", "application/json");
    xhr.setRequestHeader('Content-type', 'application/json');

    xhr.onload = function() {
        const response = this.responseText

        const obj = JSON.parse(response.toString());

        var averageReadingTime = obj[firstUser]["averageReadingTimePerForum"]
        var globalReadingTime = obj[firstUser]["globalReadingTimePerForum"]
        var msgCompletelyRead = obj[firstUser]["messagesPartiallyRead"]
        var msgPartiallyRead = obj[firstUser]["messagesCompletelyRead"]


        divAverage = createBarChart(Object.keys(averageReadingTime),Object.values(averageReadingTime))
        document.getElementById("divAverage").appendChild(divAverage)

        divGlobal = createBarChart(Object.keys(globalReadingTime),Object.values(globalReadingTime))
        document.getElementById("divGlobal").appendChild(divGlobal)

        data = [msgCompletelyRead,msgPartiallyRead]
        labels = ["Messages completely read", "Messages partially read"]
        divMsgRead = createPieChart(labels,data)
        document.getElementById("divMessages").appendChild(divMsgRead)
    }

    lst = ["averageReadingTime","globalReadingTime","postStats","readingCompletionStats"]
    const parameters = JSON.stringify({"indicators": lst});
    xhr.send(parameters);
});


