function createBarChart(labels,data,labelName){
    const div = document.createElement('div');
    const ctx = document.createElement("canvas")

    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: labelName,
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

function createPieChart(labels,data,labelName){

    const div = document.createElement('div');
    const ctx = document.createElement("canvas")

    new Chart(ctx, {
        type: 'pie',
        data: {
          labels: labels,
          datasets: [{
            label: labelName,
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

    div.appendChild(ctx)
    return div
}

function loadChartUser(user){
    firstUser = "tdelille"
    const xhr = new XMLHttpRequest();
    xhr.open("POST", "/indicators/" + user, true);
    xhr.getResponseHeader("Content-type", "application/json");
    xhr.setRequestHeader('Content-type', 'application/json');

    xhr.onload = function() {
        const response = this.responseText

        const obj = JSON.parse(response.toString());

        var averageReadingTime = obj[user]["averageReadingTimePerForum"]
        var globalReadingTime = obj[user]["globalReadingTimePerForum"]
        var msgCompletelyRead = obj[user]["messagesPartiallyRead"]
        var msgPartiallyRead = obj[user]["messagesCompletelyRead"]
        var nbMsgPosted = obj[user]["postedMsgNumber"]


        divAverage = createBarChart(Object.keys(averageReadingTime),Object.values(averageReadingTime),
        "Average reading time")
        document.getElementById("divAverage").innerHTML = ""
        document.getElementById("divAverage").appendChild(divAverage)

        divGlobal = createBarChart(Object.keys(globalReadingTime),Object.values(globalReadingTime),
        "Global reading time")
        document.getElementById("divGlobal").innerHTML = ""
        document.getElementById("divGlobal").appendChild(divGlobal)

        data = [msgCompletelyRead,msgPartiallyRead]
        labels = ["Messages completely read", "Messages partially read"]
        divMsgRead = createPieChart(labels,data,"Messages read")
        document.getElementById("divMessages").innerHTML = ""
        document.getElementById("divMessages").appendChild(divMsgRead)

        document.getElementById("nbMsgPosted").innerHTML = Math.floor(nbMsgPosted)
    }

    lst = ["averageReadingTime","globalReadingTime","postStats","readingCompletionStats"]
    const parameters = JSON.stringify({"indicators": lst});
    xhr.send(parameters);
}

window.addEventListener("DOMContentLoaded", (event) => {
    const http = new XMLHttpRequest();
    http.open("GET", "/users", true);
    http.getResponseHeader("Content-type", "application/json");

    http.onload = function() {
        const response = this.responseText

        const users = JSON.parse(response.toString());
        const selectUser = document.getElementById("selectUser")
        for (var i = 0; i < users.length; i++) {
            var option = document.createElement("option")
            if(i == 0){
                option.setAttribute("selected","selected")
            }
            option.innerHTML = users[i]
            selectUser.appendChild(option)
        }
        loadChartUser(users[0])
    }

    http.send();
});

document.getElementById("selectUser").addEventListener('change', (event) => {
    loadChartUser(event.target.value)
});


