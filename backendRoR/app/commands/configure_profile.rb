class ConfigureProfile
    prepend SimpleCommand

    def initialize(headers = {}, name, surname, age, weight)
        @headers = headers
        @name = name
        @surname = surname
        @age = age
        @weight = weight
    end

    def call
        complete
    end

    private

    attr_accessor :headers

    def complete
        if check_token
            @user ||= User.find(decoded_auth_token[:user_id]) if decoded_auth_token
            if @user
                @user.name = @name
                @user.surname = @surname
                @user.age = @age
                @user.weight = @weight
                @user.save
            else
                errors.add(:user, 'User not found')
            end
        else
        end
    end

    def user
        @user ||= User.find(decoded_auth_token[:user_id]) if decoded_auth_token
        @user || errors.add(:token, 'Invalid token') && nil
    end

    def check_token
        @decoded_token = decoded_auth_token
        print @decoded_auth_token[:exp]
        if @decoded_token[:exp] < Time.now.to_i
            errors.add(:token, 'Token expired, please login again')
            return false
        elsif BlacklistedToken.exists?(token: http_auth_header)
            errors.add(:token, 'Token in blacklist, please login again')
            return false
        else
            return true
        end
    end

    def decoded_auth_token
        @decoded_auth_token ||= JsonWebToken.decode(http_auth_header)
    end
  
    def http_auth_header

        if headers['Authorization'].present?
            return headers['Authorization'].split(' ').last
        else
            errors.add(:token, 'Missing token')
        end
        nil
    end
end