package re.smartcity.sun;

public record SunControlCommand(SunControlCommands command, Object value) {

    public Boolean getValueAsBoolean() {
        return (Boolean) value;
    }

    public Integer getValueAsInt() {
        return (Integer) value;
    }

    @Override
    public boolean equals(Object obj) {
        // !!! ВНИАМЕНИЕ: проверяю только команды
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SunControlCommand)) {
            return false;
        }
        return ((SunControlCommand) obj).command == this.command;
    }

    @Override
    public int hashCode() {
        // !!! ВНИМАНИЕ: строится только по команде
        return this.command.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", this.command, this.value);
    }
}
